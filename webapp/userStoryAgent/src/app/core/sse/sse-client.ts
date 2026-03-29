import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SseClient {
  stream<T = unknown>(url: string): Observable<T> {
    return new Observable<T>((subscriber) => {
      const source = new EventSource(url);

      source.onmessage = (event: MessageEvent<string>) => {
        try {
          subscriber.next(JSON.parse(event.data) as T);
        } catch {
          subscriber.next((event.data as unknown) as T);
        }
      };

      source.onerror = () => {
        source.close();
        subscriber.error(new Error(`SSE connection failed for ${url}`));
      };

      return () => source.close();
    });
  }
}
